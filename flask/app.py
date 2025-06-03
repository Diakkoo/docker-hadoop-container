from flask import Flask, request, jsonify, send_file, render_template
import socket
import requests
import os
from hdfs import InsecureClient
import logging
from io import BytesIO
import time

app = Flask(__name__)
app.logger.setLevel(logging.INFO)

HDFS_NAMENODE_HOST = os.getenv('HDFS_NAMENODE_HOST', 'nn')
HDFS_WEB_PORT = os.getenv('HDFS_WEB_PORT', '9870')
HDFS_RPC_PORT = os.getenv('HDFS_RPC_PORT', '9000')
HDFS_USER = os.getenv('HDFS_USER', 'hadoop')  

hdfs_client = InsecureClient(f'http://{HDFS_NAMENODE_HOST}:{HDFS_WEB_PORT}', user=HDFS_USER)

@app.route('/')
def index():
    return render_template("index.html")


""" 用 curl -X POST -F "file=@test.txt" http://localhost:5000/upload 上传文件 """

@app.route('/upload', methods=['POST'])
def upload_file():
    """上传文件到 HDFS"""

    # 检查是否有文件上传
    if 'file' not in request.files:
        return jsonify({'error': 'No file part'}), 400
    
    file = request.files['file']
    
    # 检查文件名
    if file.filename == '':
        return jsonify({'error': 'No selected file'}), 400
    
    try:
        # 获取文件内容并计算大小
        file_content = file.read()
        file_size = len(file_content)
        file_stream = BytesIO(file_content)
        file_filename = f"{int(time.time())}_{file.filename}"  # 添加时间戳以避免文件名冲突
        
        # 创建用户目录（如果不存在）
        user_dir = f'/home/hadoop/hdfs/name'
        if not hdfs_client.status(user_dir, strict=False):
            hdfs_client.makedirs(user_dir)
            app.logger.info(f"Created HDFS directory: {user_dir}")
        
        # 设置 HDFS 文件路径
        hdfs_path = f'{user_dir}/{file_filename}'
        
        # 上传到 HDFS（覆盖已存在的文件）
        hdfs_client.write(hdfs_path, file_stream, overwrite=True)
        
        # 获取上传后的文件信息
        file_status = hdfs_client.status(hdfs_path)
        
        return jsonify({
            'message': 'File uploaded successfully',
            'hdfs_path': hdfs_path,
            'file_size': file_size,
            'hdfs_size': file_status['length']
        }), 200
    
    except Exception as e:
        app.logger.error(f"Upload failed: {str(e)}")
        return jsonify({'error': str(e)}), 500


""" curl -O -J "http://your-server:5000/download/home/hadoop/hdfs/name/{filename}" """
    
@app.route('/download/<path:hdfs_path>', methods=['GET'])
def download_file(hdfs_path):
    """从HDFS下载文件到本地"""
    try:
        if not hdfs_path.startswith('/'):
            hdfs_path = '/' + hdfs_path

        # 检查文件是否存在
        if not hdfs_client.status(hdfs_path, strict=False):
            return jsonify({'error': f'File not found: {hdfs_path}'}), 404
        
        # 从HDFS读取文件内容
        with hdfs_client.read(hdfs_path) as reader:
            file_content = reader.read()
        
        # 创建内存文件对象
        file_stream = BytesIO(file_content)
        file_stream.seek(0)  # 重置指针位置
        
        # 获取文件名用于下载
        filename = os.path.basename(hdfs_path)
        
        # 作为附件发送给客户端
        return send_file(
            file_stream,
            as_attachment=True,
            download_name=filename,
            mimetype='application/octet-stream'
        )
    
    except Exception as e:
        app.logger.error(f"Download failed for {hdfs_path}: {str(e)}")
        return jsonify({'error': f'Download failed: {str(e)}'}), 500


@app.route('/test-webhdfs')
def test_webhdfs():
    """测试 WebHDFS 连接"""
    try:
        url = f'http://{HDFS_NAMENODE_HOST}:{HDFS_WEB_PORT}/webhdfs/v1/?op=LISTSTATUS'
        app.logger.info(f"Testing WebHDFS connection to: {url}")
        response = requests.get(url, timeout=5)
        return jsonify({
            "status": "success",
            "service": "WebHDFS",
            "endpoint": url,
            "response_code": response.status_code,
            "response_text": response.text[:200] + "..." if response.text else ""
        }), 200
    except Exception as e:
        app.logger.error(f"WebHDFS connection failed: {str(e)}")
        return jsonify({
            "status": "error",
            "service": "WebHDFS",
            "error": str(e)
        }), 500

@app.route('/test-rpc')
def test_rpc():
    """测试 HDFS RPC 连接"""
    try:
        app.logger.info(f"Testing RPC connection to: {HDFS_NAMENODE_HOST}:{HDFS_RPC_PORT}")
        with socket.create_connection((HDFS_NAMENODE_HOST, int(HDFS_RPC_PORT)), timeout=5) as sock:
            return jsonify({
                "status": "success",
                "service": "HDFS RPC",
                "endpoint": f"{HDFS_NAMENODE_HOST}:{HDFS_RPC_PORT}",
                "message": "TCP connection established"
            }), 200
    except Exception as e:
        app.logger.error(f"RPC connection failed: {str(e)}")
        return jsonify({
            "status": "error",
            "service": "HDFS RPC",
            "error": str(e)
        }), 500

@app.route('/test-dns')
def test_dns():
    """测试 DNS 解析"""
    try:
        app.logger.info(f"Testing DNS resolution for: {HDFS_NAMENODE_HOST}")
        ip = socket.gethostbyname(HDFS_NAMENODE_HOST)
        return jsonify({
            "status": "success",
            "host": HDFS_NAMENODE_HOST,
            "resolved_ip": ip
        }), 200
    except Exception as e:
        app.logger.error(f"DNS resolution failed: {str(e)}")
        return jsonify({
            "status": "error",
            "host": HDFS_NAMENODE_HOST,
            "error": str(e)
        }), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)

