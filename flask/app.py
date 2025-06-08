import random
import socket
import os
import requests
import logging
import time
import threading
from hdfs import InsecureClient
from flask import Flask, request, jsonify, send_file, render_template
from io import BytesIO

app = Flask(__name__)
app.logger.setLevel(logging.INFO)

# ����ȫ�ֱ���������������
generating_flag = False;
stop_event = threading.Event()

# ����WebHDFS���Ӳ���
HDFS_NAMENODE_HOST = os.getenv('HDFS_NAMENODE_HOST', 'nn')
HDFS_WEB_PORT = os.getenv('HDFS_WEB_PORT', '9870')
HDFS_RPC_PORT = os.getenv('HDFS_RPC_PORT', '9000')
HDFS_USER = os.getenv('HDFS_USER', 'hadoop')  

hdfs_client = InsecureClient(f'http://{HDFS_NAMENODE_HOST}:{HDFS_WEB_PORT}', user=HDFS_USER)

@app.route('/')
def index():
    return render_template("index.html")

'''��̨�������ݺ���'''

def generate_data_thread():
    global generating_flag

    try:
        with open("//app/generate/data.txt", mode="a") as wrt:
            ID = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k']
            actionCode = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 'l', 'm', 'n', 'o', 'p', 'q']

            while generating_flag and not stop_event.is_set():
                ip_part = ''.join(random.sample(ID, 4))

                time_part = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
                
                action_elements = random.sample(actionCode, 4)
                action_part = ''.join(str(x) for x in action_elements)

                g_line = f"{ip_part}  {time_part}  {action_part}\n"
                wrt.write(g_line)
                wrt.flush()
                time.sleep(0.5)
    except Exception as e:
        app.logger.error(f"Error in data generation thread: {str(e)}")

''' curl -X GET "http://localhost:5000/generate/1" ��ʼ�������� '''
''' curl -X GET "http://localhost:5000/generate/2" ֹͣ�������� '''

@app.route('/generate/<int:trg>', methods=['GET'])
def generate_data(trg):
    global generating_flag, stop_event

    if trg == 0:            # ���URL�������Ϊ0������ֹͣ�¼���ֹͣ��������
        if generating_flag:
            generating_flag = False
            stop_event.set()
            return jsonify({"status": "data generating stopped"}), 200
    elif trg == 1:          # ���URL�������Ϊ1����ʼ��������
        if not generating_flag:
            generating_flag = True
            stop_event.clear()
            thread = threading.Thread(target=generate_data_thread)
            thread.start()
            return jsonify({"status": "data generating started"}), 200
    else:
        return jsonify({"error": "Invalid parameter. Use 1 to start and 0 to stop."}), 400

"""�ϴ��ļ��� HDFS"""
""" �� curl -X POST -F "file=@test.txt" http://localhost:5000/upload �ϴ��ļ� """

@app.route('/upload', methods=['POST'])
def upload_file():

    # ����Ƿ����ļ��ϴ�
    if 'file' not in request.files:
        return jsonify({'error': 'No file part'}), 400
    
    file = request.files['file']
    
    # ����ļ���
    if file.filename == '':
        return jsonify({'error': 'No selected file'}), 400
    
    try:
        # ��ȡ�ļ����ݲ������С
        file_content = file.read()
        file_size = len(file_content)
        file_stream = BytesIO(file_content)
        file_filename = f"{int(time.time())}_{file.filename}"  # ���ʱ����Ա����ļ�����ͻ
        
        # �����û�Ŀ¼����������ڣ�
        user_dir = f'/home/hadoop/local_input'
        if not hdfs_client.status(user_dir, strict=False):
            hdfs_client.makedirs(user_dir)
            app.logger.info(f"Created HDFS directory: {user_dir}")
        
        # ���� HDFS �ļ�·��
        hdfs_path = f'{user_dir}/{file_filename}'
        
        # �ϴ��� HDFS�������Ѵ��ڵ��ļ���
        hdfs_client.write(hdfs_path, file_stream, overwrite=True)
        
        # ��ȡ�ϴ�����ļ���Ϣ
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


""" curl -O -J "http://localhost:5000/download/home/hadoop/hdfs/name/{filename}" """
    
@app.route('/download/<path:hdfs_path>', methods=['GET'])
def download_file(hdfs_path):
    """��HDFS�����ļ�������"""
    try:
        if not hdfs_path.startswith('/'):
            hdfs_path = '/' + hdfs_path

        # ����ļ��Ƿ����
        if not hdfs_client.status(hdfs_path, strict=False):
            return jsonify({'error': f'File not found: {hdfs_path}'}), 404
        
        # ��HDFS��ȡ�ļ�����
        with hdfs_client.read(hdfs_path) as reader:
            file_content = reader.read()
        
        # �����ڴ��ļ�����
        file_stream = BytesIO(file_content)
        file_stream.seek(0)  # ����ָ��λ��
        
        # ��ȡ�ļ�����������
        filename = os.path.basename(hdfs_path)
        
        # ��Ϊ�������͸��ͻ���
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
    """���� WebHDFS ����"""
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
    """���� HDFS RPC ����"""
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
    """���� DNS ����"""
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
    app.run(host='0.0.0.0', port=5000, debug=True)
