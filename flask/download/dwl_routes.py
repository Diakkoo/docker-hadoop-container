from flask import Blueprint, jsonify, send_file
from app import HDFS_CLIENT
from io import BytesIO
import os
import logging

download_bp = Blueprint('download', __name__)

"""从HDFS下载文件到本地"""
""" curl -O -J "http://localhost:5000/download/home/hadoop/hdfs/name/{filename}" """

@download_bp.route('/download/<path:hdfs_path>', methods = ['GET'])
def download_file(hdfs_path):
    global HDFS_CLIENT

    try:
        if not hdfs_path.startswith('/'):
            hdfs_path = '/' + hdfs_path

        # 检查文件是否存在
        if not HDFS_CLIENT.status(hdfs_path, strict = False):
            return jsonify(
                {
                    "error" : f"file not found; {hdfs_path}"
                }
            ), 404
        
        # 从 HDFS 读取文件内容
        with HDFS_CLIENT.read(hdfs_path) as reader:
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
        download_bp.logger.error(f"Download failed for {hdfs_path}: {str(e)}")
        return jsonify(
            {
                'error': f'Download failed: {str(e)}'
            }
            ), 500