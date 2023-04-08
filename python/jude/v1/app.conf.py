import multiprocessing
import os


bind = "0.0.0.0:8000"

threads = int(os.getenv("THREAD_COUNT") or 3)
workers = 1
while True:
    if workers*threads > (multiprocessing.cpu_count() +1) * 2:
        if workers > 1:
            workers -= 1
        break
    else:
        workers += 1

preload_app = True
