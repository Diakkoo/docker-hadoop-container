import os
import logging
import threading
from hdfs import InsecureClient
from flask import Flask, render_template
from connection.conn_routes import connection_bp
from download.dwl_routes import download_bp
from generate.gnr_routes import generate_bp
from upload.upl_routes import upload_bp

app = Flask(__name__)
app.logger.setLevel(logging.INFO)

# 定义全局变量控制数据生成
generating_flag = False;
stop_event = threading.Event()

# 配置WebHDFS连接参数
HDFS_NAMENODE_HOST = os.getenv('HDFS_NAMENODE_HOST', 'nn')
HDFS_WEB_PORT = os.getenv('HDFS_WEB_PORT', '9870')
HDFS_RPC_PORT = os.getenv('HDFS_RPC_PORT', '9000')
HDFS_USER = os.getenv('HDFS_USER', 'hadoop')  

HDFS_CLIENT = InsecureClient(f'http://{HDFS_NAMENODE_HOST}:{HDFS_WEB_PORT}', user=HDFS_USER)

app.register_blueprint(
    connection_bp,
    url_prefix = '/connection'
)

app.register_blueprint(
    download_bp,
    url_prefix = '/download'
)

app.register_blueprint(
    generate_bp,
    url_prefix = '/generate'
)

app.register_blueprint(
    upload_bp,
    url_prefix = '/upload'
)

@app.route('/')
def index():
    return render_template("/templates/index.html")

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
