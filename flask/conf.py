import os
from hdfs import InsecureClient

# 配置WebHDFS连接参数
HDFS_NAMENODE_HOST = os.getenv('HDFS_NAMENODE_HOST', 'nn')
HDFS_WEB_PORT = os.getenv('HDFS_WEB_PORT', '9870')
HDFS_RPC_PORT = os.getenv('HDFS_RPC_PORT', '9000')
HDFS_USER = os.getenv('HDFS_USER', 'hadoop')  

HDFS_CLIENT = InsecureClient(f'http://{HDFS_NAMENODE_HOST}:{HDFS_WEB_PORT}', user=HDFS_USER)
