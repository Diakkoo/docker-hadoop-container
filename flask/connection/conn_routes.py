from flask import Blueprint, jsonify
from app import HDFS_NAMENODE_HOST, HDFS_WEB_PORT, HDFS_RPC_PORT, HDFS_USER
import requests
import socket
import logging

connection_bp = Blueprint('connection', __name__)

@connection_bp.route('/connection/test-webhdfs')
def test_webhdfs():
    global HDFS_NAMENODE_HOST, HDFS_WEB_PORT

    """ 测试 WebHDFS 连接 """
    try:
        url = f'http://{HDFS_NAMENODE_HOST}:{HDFS_WEB_PORT}/webhdfs/v1/?op=LISTSTATUS'
        connection_bp.logger.info(f"Testing WebHDFS connection to: {HDFS_NAMENODE_HOST}:{HDFS_WEB_PORT}")
        response = requests.get(url, timeout = 10)
        return jsonify(
            {
            "status": "success",
            "service": "WebHDFS",
            "endpoint": url,
            "response_code": response.status_code,
            "response_text": response
        }
        ),200
    except Exception as e:
        connection_bp.logger.error(f"WebHDFS connection faild: {str(e)}")
        return jsonify(
            {"status": 'error',
             "service": "WebHDFS",
             "error": str(e)
        }
        ), 500
    
@connection_bp.route('/connection/test-rpc')
def test_rpc():
    global HDFS_NAMENODE_HOST, HDFS_RPC_PORT

    """ 测试 HDFS RPC 连接 """
    try:
        connection_bp.logger.info(f"Testing RPC connection to: {HDFS_NAMENODE_HOST}:{HDFS_RPC_PORT}")
        with socket.create_connection((HDFS_NAMENODE_HOST, int(HDFS_RPC_PORT)), timeout=5) as sock:
            return jsonify({
                "status": "success",
                "service": "HDFS RPC",
                "endpoint": f"{HDFS_NAMENODE_HOST}:{HDFS_RPC_PORT}",
                "message": "TCP connection established"
            }), 200
    except Exception as e:
        connection_bp.logger.error(f"RPC connection failed: {str(e)}")
        return jsonify({
            "status": "error",
            "service": "HDFS RPC",
            "error": str(e)
        }), 500
    
@connection_bp.route('/connection/test-dns')
def test_dns():
    global HDFS_NAMENODE_HOST

    """" 测试 DNS 解析 """
    try:
        connection_bp.logger.info(f"Testing DNS resolution for: {HDFS_NAMENODE_HOST}")
        ip = socket.gethostbyname(HDFS_NAMENODE_HOST)
        return jsonify({
            "status": "success",
            "host": HDFS_NAMENODE_HOST,
            "resolved_ip": ip
        }), 200
    except Exception as e:
        connection_bp.logger.error(f"DNS resolution failed: {str(e)}")
        return jsonify({
            "status": "error",
            "host": HDFS_NAMENODE_HOST,
            "error": str(e)
        }), 500