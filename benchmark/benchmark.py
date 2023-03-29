import json
import time
import random
import requests
import subprocess
from dataclasses import dataclass
from typing import List


def headers():
    return {"Content-Type": "application/json"}


@dataclass
class Sample:
    clock: float
    latencies: List[int]
    power_usage: int


class BenchmarkBase:
    def __init__(self, server_host, server_start_cmd = None, database_start_cmd = None, api_port=8080, scaphandre_port=8081):
        self.server_host = server_host
        self.api_port = api_port
        self.scaphandre_port = scaphandre_port
        self.database_process = None
        self.server_process = None
        self.server_start_cmd = server_start_cmd
        self.database_start_cmd = database_start_cmd

    def request_product(self, product_id):
        return requests.get(f'http://{self.server_host}:{self.api_port}/products/{product_id}').json()

    def make_request(self):
        t0 = time.time_ns()
        product_list = requests.get(f'http://{self.server_host}:{self.api_port}/products').json()
        random.shuffle(product_list)
        selected = product_list[:5]
        total = 0
        to_order = []
        for product in selected:
            total += 10 * self.request_product(product['id'])['price']
            to_order.append({"productId": product['id'], "amount": 10})

        order_id = int(requests.request(method='POST',
                                        url=f'http://{self.server_host}:{self.api_port}/orders/new',
                                        headers=headers(), data=json.dumps(to_order)).text)
        order = requests.get(f'http://{self.server_host}:{self.api_port}/orders/{order_id}').json()
        time_used = (time.time_ns() - t0) // 1000000
        if order['total'] - total > 0.0001:
            raise Exception(f'computed total and reported total differ: {order["total"] - total}')
        return time_used

    def energy_usage(self):
        for line in requests.get(f'http://{self.server_host}:{self.scaphandre_port}/metrics').text.splitlines(
                keepends=False):
            if line.startswith('scaph_host_energy_microjoules'):
                return int(line.split(' ')[1])

    def baseline_energy_usage(self, duration_minutes=60):
        be = self.energy_usage()
        time.sleep(duration_minutes * 60)
        return self.energy_usage() - be

    def warmup(self, duration_minutes=10):
        baseline = self.baseline_energy_usage(duration_minutes)
        print(f'baseline stablished: {baseline}uj')
        termination = time.time() + duration_minutes * 60
        self.start_database()
        start = self.energy_usage()
        self.start_remote_server()
        latencies = []
        while time.time() < termination:
            latencies.append(self.make_request())
            time.sleep(1)
            pass
        total_energy = self.energy_usage() - start - baseline
        return Sample(time.time(), latencies, total_energy)

    def start_remote_server(self):
        if self.server_process:
            self.server_process.terminate()
        if self.server_start_cmd:
            print('starting server')
            self.server_process = subprocess.Popen(self.server_start_cmd, stdout=subprocess.PIPE)
            time.sleep(10)  # sleep a little so the database has time to start
        else:
            print('not starting server')

    def start_database(self):
        if self.database_process:
            self.database_process.terminate()
        if self.database_start_cmd:
            print('starting database')
            self.database_process = subprocess.Popen(self.database_start_cmd, stdout=subprocess.PIPE)
            time.sleep(10) # sleep a little so the database has time to start
        else:
            print('not starting database')


if __name__ == '__main__':
    server = ['java', '-jar', '/home/takeshi/projects/github/power-usage-comparisons/java/quarkus-sample-rest/build'
                              '/quarkus-app/quarkus-run.jar']
    b = BenchmarkBase(server_host='localhost', server_start_cmd=server)

    # b.start_remote_server()
    # time.sleep(30)
    # b.server_process.terminate()
    print(b.warmup(2))
