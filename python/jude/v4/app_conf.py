import multiprocessing


bind = "0.0.0.0:8000"

worker_class = "uvloop"
workers = multiprocessing.cpu_count()
