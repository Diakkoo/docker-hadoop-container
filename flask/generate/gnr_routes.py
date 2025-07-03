import time
import threading
import csv
import random
from flask import jsonify, Blueprint, current_app

generate_bp = Blueprint('generate', __name__)

# 定义全局变量控制数据生成
generating_flag = False;
stop_event = threading.Event()

'''后台生成数据函数'''
def generate_data_thread():
    global generating_flag
    # time_stamp = f"{int(time.time())}"
    try:
        with open(f"/app/generate/data.csv", mode="a") as wrt:
            ID = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
            actionCode = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 'l', 'm', 'n', 'o', 'p', 'q']

            while generating_flag and not stop_event.is_set():
                ip_part = ''.join(map(str, random.sample(ID, 6)))

                date_part = time.strftime("%Y-%m-%d", time.localtime())
                time_part = time.strftime("%H:%M:%S", time.localtime())
                
                action_elements = random.sample(actionCode, 4)
                action_part = ''.join(str(x) for x in action_elements)

                resource_part = random.randint(-9999, 9999999)
                if_returned = random.choice([True, False])
                if if_returned:
                    returned_resource = random.randint(1, resource_part)
                else:
                    returned_resource = 0

                wrt_csv = csv.writer(wrt)
                wrt_csv.writerow([
                    ip_part,
                    date_part,
                    time_part,
                    action_part,
                    resource_part,
                    if_returned,
                    returned_resource])
                wrt.flush()
                time.sleep(0.05)
    except Exception as e:
        current_app.logger.error(f"Error in data generation thread: {str(e)}")

''' curl -X GET "http://localhost:5000/generate/1" 开始生成数据 '''
''' curl -X GET "http://localhost:5000/generate/0" 停止生成数据 '''
@generate_bp.route('/<int:trg>', methods=['GET'])
def generate_data(trg):
    global generating_flag, stop_event

    if trg == 0:            # 如果URL传入参数为0，设置停止事件，停止数据生成
        if generating_flag:
            generating_flag = False
            stop_event.set()
            return jsonify({"status": "data generating stopped"}), 200
    elif trg == 1:          # 如果URL传入参数为1，开始数据生成
        if not generating_flag:
            generating_flag = True
            stop_event.clear()
            thread = threading.Thread(target=generate_data_thread)
            thread.start()
            return jsonify({"status": "data generating started"}), 200
    else:
        return jsonify({"error": "Invalid parameter. Use 1 to start and 0 to stop."}), 400