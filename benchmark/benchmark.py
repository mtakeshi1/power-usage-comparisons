import json
import os.path
import time
import random

# import plotext
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

    def p99(self):
        return self.percentile(0.99)

    def median(self):
        return self.percentile(0.5)

    def percentile(self, perc):
        sorted(self.latencies)
        part = int(perc * len(self.latencies))
        if part >= len(self.latencies):
            return self.latencies[-1]
        else:
            return self.latencies[part]

    def avg(self):
        return sum(self.latencies) / len(self.latencies)

    def __str__(self):
        return f'samples: {len(self.latencies)}, power: {self.power_usage}, p99: {self.p99()}, median: {self.median()}, avg: {self.avg()}'


class ServerProcess:

    def __init__(self, name, start_cmd, stop_cmd=None):
        self.name = name
        self.start_cmd = start_cmd
        self.stop_cmd = stop_cmd
        self.process = None

    def start(self):
        if self.process is not None:
            self.stop()
        self.process = subprocess.Popen(self.start_cmd, stdout=None, stderr=None, stdin=None)
        time.sleep(10)  # sleep a little so the database has time to start

    def stop(self):
        if self.process:
            if self.stop_cmd:
                subprocess.run(self.stop_cmd, stdout=None, stderr=None, timeout=20, stdin=None)
            else:
                self.process.terminate()
                time.sleep(10)

    def remote(self, host):
        if self.stop_cmd:
            return ServerProcess(self.name, ['ssh', host] + self.start_cmd, ['ssh', host] + self.stop_cmd)
        else:
            return ServerProcess(self.name, ['ssh', host] + self.start_cmd)


def docker_process(name, image, env=None, port_from=8080, port_to=8080, additional_args=None):
    if additional_args is None:
        additional_args = []
    cmds = ['docker', 'run', '--rm', '--name', name, f'-p{port_from}:{port_to}', '-d']
    if env and os.path.exists(env):
        cmds.append('--env-file')
        cmds.append(env)
    for e in additional_args:
        cmds.append(e)
    cmds.append(image)
    return ServerProcess(name, cmds, ['docker', 'stop', name])


def new_database_process():
    return docker_process('pgsql', 'power/pgsql', '.env', port_from=5432, port_to=5432)


class BenchmarkBase:
    def __init__(self, server_host, server_process: ServerProcess, database_process: ServerProcess, api_port=8080, scaphandre_port=8081):
        self.server_host = server_host
        self.api_port = api_port
        self.scaphandre_port = scaphandre_port
        self.server_process = server_process
        self.database_process = database_process

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
        baseline = 0
        # baseline = self.baseline_energy_usage(duration_minutes)
        # print(f'baseline stablished: {baseline}uj')
        termination = time.time() + duration_minutes * 60
        self.start_database()
        try:
            start = self.energy_usage()
            self.start_remote_server()
            latencies = []
            while time.time() < termination:
                latencies.append(self.make_request())
                time.sleep(0.5)
                pass
            total_energy = self.energy_usage() - start - baseline
        finally:
            self.stop_server()
            self.stop_database()
        return Sample(time.time(), latencies, total_energy)

    def start_remote_server(self):
        self.server_process.start()

    def start_database(self):
        self.database_process.start()

    def stop_server(self):
        self.server_process.stop()

    def stop_database(self):
        self.database_process.stop()


def quarkus_process():
    server = docker_process('java-quarkus', 'power/java', env='.env')
    return BenchmarkBase(server_host='localhost', server_process=server, database_process=new_database_process())


def server_jude():
    server = docker_process('java-quarkus', 'power/java', port_to=8000, env='.env')
    return BenchmarkBase(server_host='localhost', server_process=server, database_process=new_database_process())


if __name__ == '__main__':
    # b = server_jude()
    b = quarkus_process()
    # b.read_env()
    r = b.warmup(2)
    print('sample quarkus' + str(r))
    plotext.hist(r.latencies, 30, label='quarkus')
    r = server_jude().warmup(2)
    print('sample python' + str(r))
    plotext.hist(r.latencies, 30, label='python-jude')
    plotext.title('latencies distribution')
    plotext.show()
    # b.start_remote_server()
    # b.make_request()
    # b.server_process.terminate()
    # time.sleep(30)
    # b.server_process.terminate()
    # print(b.warmup(2))
