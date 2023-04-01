import multiprocessing


bind = "0.0.0.0:8000"

worker_class = 'sync'
workers = multiprocessing.cpu_count() * 2 + 1

preload_app = False
reload = True
reload_engine = 'auto'
accesslog = '-'
loglevel = 'debug'
