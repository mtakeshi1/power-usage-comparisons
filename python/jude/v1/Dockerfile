FROM python:3.11

ENV DB_CONNECTION_STRING=???LOL???

WORKDIR /usr/app

COPY app.conf.py /usr/app/
COPY app.py /usr/app/
COPY requirements.txt .
RUN pip install -r requirements.txt


CMD ["gunicorn", "--config=app.conf.py", "app:create"]