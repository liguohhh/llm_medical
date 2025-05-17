import json
import os
from typing import Dict, List, Optional, Union, Any
from pathlib import Path
from collections import OrderedDict

class PromptManager:
    def __init__(self, prompt_dir: str = "llm/prompts/templates"):
        self.prompt_dir = Path(prompt_dir)
        self.prompt_dir.mkdir(parents=True, exist_ok=True)

    def _get_template_path(self, template_id: str) -> Path:
        """获取模板文件的完整路径"""
        return self.prompt_dir / f"{template_id}.json"

    def get_template(self, template_id: str) -> Optional[Dict]:
        """获取指定ID的大模板文件
        
        Args:
            template_id: 大模板ID
            
        Returns:
            Dict: 包含多个小模板的大模板数据
        """
        file_path = self._get_template_path(template_id)
        if not file_path.exists():
            return None
            
        try:
            with open(file_path, "r", encoding="utf-8") as f:
                template_data = json.load(f)
                
                # 按照order字段对子模板进行排序
                if "sub_templates" in template_data and template_data["sub_templates"]:
                    # 创建子模板项列表，包含名称和子模板数据
                    sub_template_items = []
                    for name, sub_template in template_data["sub_templates"].items():
                        # 如果没有order字段，设置一个默认值（较大的数，放在末尾）
                        order = sub_template.get("order", 999)
                        sub_template_items.append((name, sub_template, order))
                    
                    # 按order字段排序
                    sub_template_items.sort(key=lambda x: x[2])
                    
                    # 重新构建有序的子模板字典
                    ordered_sub_templates = OrderedDict()
                    for name, sub_template, _ in sub_template_items:
                        ordered_sub_templates[name] = sub_template
                    
                    template_data["sub_templates"] = ordered_sub_templates
                
                return template_data
        except Exception as e:
            print(f"读取大模板 {template_id} 时出错: {e}")
            return None

    def add_template(self, template_id: str, template_data: Dict) -> bool:
        """添加新的大模板
        
        Args:
            template_id: 大模板ID
            template_data: 大模板数据，格式为：
                {
                    "description": "大模板描述",
                    "sub_templates": {
                        "sub_id_1": {
                            "template": "模板1内容",
                            "description": "模板1描述",
                            "parameters": ["param1", "param2"],
                            "order": 1
                        },
                        "sub_id_2": {
                            "template": "模板2内容",
                            "description": "模板2描述",
                            "parameters": ["param1", "param3"],
                            "order": 2
                        }
                    }
                }
                
        Returns:
            bool: 是否添加成功
        """
        file_path = self._get_template_path(template_id)
        if file_path.exists():
            return False
            
        try:
            # 确保每个子模板都有order字段
            if "sub_templates" in template_data:
                for name, sub_template in template_data["sub_templates"].items():
                    if "order" not in sub_template:
                        # 如果没有提供order，则按照添加顺序设置
                        sub_template["order"] = list(template_data["sub_templates"].keys()).index(name) + 1
            
            with open(file_path, "w", encoding="utf-8") as f:
                json.dump(template_data, f, ensure_ascii=False, indent=2)
            return True
        except Exception as e:
            print(f"保存大模板 {template_id} 时出错: {e}")
            return False

    def update_template(self, template_id: str, template_data: Dict) -> bool:
        """更新现有大模板
        
        Args:
            template_id: 大模板ID
            template_data: 更新后的大模板数据
                
        Returns:
            bool: 是否更新成功
        """
        file_path = self._get_template_path(template_id)
        if not file_path.exists():
            return False
            
        try:
            # 确保每个子模板都有order字段
            if "sub_templates" in template_data:
                for name, sub_template in template_data["sub_templates"].items():
                    if "order" not in sub_template:
                        # 如果没有提供order，则按照添加顺序设置
                        sub_template["order"] = list(template_data["sub_templates"].keys()).index(name) + 1
            
            with open(file_path, "w", encoding="utf-8") as f:
                json.dump(template_data, f, ensure_ascii=False, indent=2)
            return True
        except Exception as e:
            print(f"更新大模板 {template_id} 时出错: {e}")
            return False

    def delete_template(self, template_id: str) -> bool:
        """删除大模板
        
        Args:
            template_id: 要删除的大模板ID
                
        Returns:
            bool: 是否删除成功
        """
        file_path = self._get_template_path(template_id)
        if not file_path.exists():
            return False
            
        try:
            file_path.unlink()
            return True
        except Exception as e:
            print(f"删除大模板 {template_id} 时出错: {e}")
            return False

    def list_templates(self) -> List[str]:
        """获取所有大模板ID列表
        
        Returns:
            List[str]: 大模板ID列表
        """
        try:
            return [f.stem for f in self.prompt_dir.glob("*.json")]
        except Exception as e:
            print(f"列出大模板时出错: {e}")
            return []

    def format_sub_template(self, template_id: str, sub_template_id: str, **kwargs) -> Optional[str]:
        """格式化大模板中的特定小模板
        
        Args:
            template_id: 大模板ID
            sub_template_id: 大模板中的小模板ID
            **kwargs: 格式化参数
                
        Returns:
            Optional[str]: 格式化后的模板文本
        """
        template_data = self.get_template(template_id)
        if not template_data or "sub_templates" not in template_data:
            return None
            
        sub_templates = template_data.get("sub_templates", {})
        if sub_template_id not in sub_templates:
            return None
            
        sub_template = sub_templates[sub_template_id]
        if "template" not in sub_template:
            return None
            
        try:
            return sub_template["template"].format(**kwargs)
        except KeyError as e:
            print(f"缺少必需参数: {e}")
            return None
        except Exception as e:
            print(f"格式化模板 {template_id}/{sub_template_id} 时出错: {e}")
            return None
            
    def format_selected_templates(self, template_id: str, sub_template_ids: List[str], **kwargs) -> Optional[str]:
        """格式化大模板中选定的多个小模板并拼接
        
        Args:
            template_id: 大模板ID
            sub_template_ids: 要使用的小模板ID列表 (已废弃，将使用大模板中的所有子模板)
            **kwargs: 格式化参数
                
        Returns:
            Optional[str]: 格式化并拼接后的模板文本
        """
        template_data = self.get_template(template_id)
        if not template_data or "sub_templates" not in template_data:
            return None
            
        sub_templates = template_data.get("sub_templates", {})
        
        # 忽略传入的sub_template_ids，使用所有子模板
        all_sub_templates = []
        for sub_id, sub_template in sub_templates.items():
            if "template" not in sub_template:
                continue
                
            # 获取排序字段，默认为999（放在末尾）
            order = sub_template.get("order", 999)
            all_sub_templates.append((sub_id, sub_template, order))
        
        # 按order字段排序
        all_sub_templates.sort(key=lambda x: x[2])
        
        formatted_parts = []
        for sub_id, sub_template, _ in all_sub_templates:
            try:
                formatted_part = sub_template["template"].format(**kwargs)
                formatted_parts.append(formatted_part)
            except KeyError as e:
                print(f"小模板 {sub_id} 缺少必需参数: {e}")
            except Exception as e:
                print(f"格式化小模板 {template_id}/{sub_id} 时出错: {e}")
        
        if not formatted_parts:
            return None
            
        return "\n\n".join(formatted_parts)
