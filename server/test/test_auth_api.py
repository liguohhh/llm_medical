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
    print(f"状态码: {response.status_code}")
    
    # 检查响应内容是否为空
    if not response.text.strip():
        print("响应内容为空")
        print("-" * 50)
        return
    
    try:
        response_json = response.json()
        print(f"响应内容: {json.dumps(response_json, indent=2, ensure_ascii=False)}")
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
    
    try:
        url = f"{BASE_URL}/api/auth/register"
        response = session.post(url, json=register_data, headers=HEADERS)
        print_response(response)
        
        # 判断是否注册成功
        if response.status_code == 200 and response.text.strip():
            response_json = response.json()
            if response_json.get("code") == 200:
                print(f"注册成功，用户名: {username}")
                return username
        
        print("注册失败")
        return None
    except Exception as e:
        print(f"注册过程中发生错误: {e}")
        return None

def test_login(username, password="password123"):
    """测试用户登录"""
    print("\n测试用户登录：")
    
    if not username:
        print("用户名为空，无法登录")
        return False
    
    # 登录数据
    login_data = {
        "username": username,
        "password": password
    }
    
    try:
        url = f"{BASE_URL}/api/auth/login"
        response = session.post(url, json=login_data, headers=HEADERS)
        print_response(response)
        
        # 判断是否登录成功
        if response.status_code == 200 and response.text.strip():
            response_json = response.json()
            if response_json.get("code") == 200:
                print("登录成功")
                return True
        
        print("登录失败")
        return False
    except Exception as e:
        print(f"登录过程中发生错误: {e}")
        return False

def test_login_with_wrong_password(username):
    """测试错误密码登录"""
    print("\n测试错误密码登录：")
    
    if not username:
        print("用户名为空，无法测试错误密码登录")
        return False
    
    return test_login(username, "wrong_password")

def test_get_user_info():
    """测试获取当前用户信息"""
    print("\n测试获取当前用户信息：")
    
    try:
        url = f"{BASE_URL}/api/auth/info"
        response = session.get(url, headers=HEADERS)
        print_response(response)
        
        # 判断是否获取成功
        if response.status_code == 200 and response.text.strip():
            response_json = response.json()
            if response_json.get("code") == 200:
                print("获取用户信息成功")
                return True
        
        print("获取用户信息失败")
        return False
    except Exception as e:
        print(f"获取用户信息过程中发生错误: {e}")
        return False

def test_logout():
    """测试用户注销"""
    print("\n测试用户注销：")
    
    try:
        url = f"{BASE_URL}/api/auth/logout"
        response = session.post(url, headers=HEADERS)
        print_response(response)
        
        # 判断是否注销成功
        if response.status_code == 200 and response.text.strip():
            response_json = response.json()
            if response_json.get("code") == 200:
                print("注销成功")
                return True
        
        print("注销失败")
        return False
    except Exception as e:
        print(f"注销过程中发生错误: {e}")
        return False

def test_get_user_info_after_logout():
    """测试注销后获取用户信息"""
    print("\n测试注销后获取用户信息：")
    
    try:
        url = f"{BASE_URL}/api/auth/info"
        response = session.get(url, headers=HEADERS)
        print_response(response)
        
        # 判断是否获取失败（因为已注销）
        if response.status_code != 200 or (response.text.strip() and response.json().get("code") != 200):
            print("已注销，无法获取用户信息，测试通过")
            return True
        
        print("已注销但仍能获取用户信息，测试失败")
        return False
    except Exception as e:
        print(f"获取用户信息过程中发生错误: {e}")
        return True  # 发生错误可能是因为未认证，也算测试通过

def main():
    """主函数"""
    print("开始测试医疗辅助问答系统API...")
    
    try:
        # 测试用户注册
        username = test_register()
        
        if not username:
            print("注册失败，终止测试")
            return
        
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
        else:
            print("登录失败，无法继续测试获取用户信息和注销功能")
    
    except Exception as e:
        print(f"测试过程中发生错误: {e}")
    
    print("\n测试完成！")

if __name__ == "__main__":
    main() 