#!/usr/bin/env python
# -*- coding: utf-8 -*-

import requests
import json
import time
import random

# API基础URL
BASE_URL = "http://localhost:8086"

# 请求头
HEADERS = {
    "Content-Type": "application/json"
}

# 会话对象，用于保持Cookie
session = requests.Session()

def print_response(response):
    """打印响应内容"""
    try:
        print(f"状态码: {response.status_code}")
        print(f"响应内容: {json.dumps(response.json(), indent=2, ensure_ascii=False)}")
        print("-" * 50)
    except Exception as e:
        print(f"解析响应失败: {e}")
        print(f"原始响应: {response.text}")
        print("-" * 50)

def test_register():
    """测试用户注册"""
    print("\n测试用户注册：")
    
    # 生成随机用户名，避免冲突
    random_suffix = random.randint(1000, 9999)
    username = f"test_user_{random_suffix}"
    
    # 注册数据
    register_data = {
        "username": username,
        "password": "password123",
        "realName": "测试用户",
        "gender": 1,
        "age": 30
    }
    
    url = f"{BASE_URL}/api/auth/register"
    response = session.post(url, json=register_data, headers=HEADERS)
    print_response(response)
    
    # 返回用户名，用于后续测试
    return username

def test_login(username, password="password123"):
    """测试用户登录"""
    print("\n测试用户登录：")
    
    # 登录数据
    login_data = {
        "username": username,
        "password": password
    }
    
    url = f"{BASE_URL}/api/auth/login"
    response = session.post(url, json=login_data, headers=HEADERS)
    print_response(response)
    
    return response.json()["code"] == 200

def test_login_with_wrong_password(username):
    """测试错误密码登录"""
    print("\n测试错误密码登录：")
    return test_login(username, "wrong_password")

def test_get_user_info():
    """测试获取当前用户信息"""
    print("\n测试获取当前用户信息：")
    
    url = f"{BASE_URL}/api/auth/info"
    response = session.get(url, headers=HEADERS)
    print_response(response)
    
    return response.json()["code"] == 200

def test_logout():
    """测试用户注销"""
    print("\n测试用户注销：")
    
    url = f"{BASE_URL}/api/auth/logout"
    response = session.post(url, headers=HEADERS)
    print_response(response)
    
    return response.json()["code"] == 200

def test_get_user_info_after_logout():
    """测试注销后获取用户信息"""
    print("\n测试注销后获取用户信息：")
    
    url = f"{BASE_URL}/api/auth/info"
    response = session.get(url, headers=HEADERS)
    print_response(response)
    
    return response.json()["code"] != 200

def main():
    """主函数"""
    print("开始测试医疗辅助问答系统API...")
    
    try:
        # 测试用户注册
        username = test_register()
        
        # 等待1秒
        time.sleep(1)
        
        # 测试错误密码登录
        test_login_with_wrong_password(username)
        
        # 等待1秒
        time.sleep(1)
        
        # 测试正确登录
        login_success = test_login(username)
        
        if login_success:
            # 登录成功，测试获取用户信息
            test_get_user_info()
            
            # 等待1秒
            time.sleep(1)
            
            # 测试注销
            logout_success = test_logout()
            
            if logout_success:
                # 测试注销后获取用户信息
                test_get_user_info_after_logout()
    
    except Exception as e:
        print(f"测试过程中发生错误: {e}")
    
    print("\n测试完成！")

if __name__ == "__main__":
    main() 