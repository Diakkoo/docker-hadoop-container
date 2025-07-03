from flask import Blueprint, jsonify, request, current_app
from io import BytesIO
from conf import HDFS_CLIENT
import time

upload_bp = Blueprint('upload', __name__)

"""上传文件到 HDFS"""
""" 用 curl -X POST -F "file=@test.txt" http://localhost:5000/upload 上传文件 """

@upload_bp.route('/', methods=['POST'])
def upload_file():
    global HDFS_CLIENT

    # 检查是否有文件上传
    if 'file' not in request.files:
        return jsonify(
            {
                "error": "no file part"
        }
        ), 400
    
    file = request.files['file']

    # 检查文件名
    if file.filename == '':
        return jsonify(
            {
                "error": "no selected file"
            }
        ), 400
    
    try:
        # 获取文件内容并计算大小
        file_content = file.read()
        file_size = len(file_content)
        file_stream = BytesIO(file_content)
        file_filename = f"{int(time.time())}_{file.filename}"  # 添加时间戳以避免文件名冲突

        remote_dir = '/remote_input'
        if not HDFS_CLIENT.status(remote_dir, strict = False):
            HDFS_CLIENT.makedirs(remote_dir)
            current_app.logger.info(f"Created HDFS directory: {remote_dir}")
        
        # 设置 HDFS 文件路径
        hdfs_path = f"{remote_dir}/{file_filename}"

        # 上传到 HDFS
        # 覆盖已存在的文件
        HDFS_CLIENT.write(hdfs_path, file_stream, overwrite = True)

        # 获取上传后文件状态
        file_status = HDFS_CLIENT.status(hdfs_path)

        return jsonify(
            {
                "message": "file uploaded successfully",
                "hdfs_path": hdfs_path,
                "file_size": file_size,
                "hdfs_size": file_status['length']
            }
        ), 200
    except Exception as e:
        current_app.logger.error(f"Upload failed: {str(e)}")
        return jsonify(
            {
                "error": str(e)
            }
        ), 500