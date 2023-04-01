FROM python:3.11

ENV DB_CONNECTION_STRING=???LOL???

RUN mkdir -p /usr/app
COPY *py /usr/app/

WORKDIR /usr/app

COPY requirements.txt .
RUN pip install -r requirements.txt


CMD ["gunicorn", "--config=app.conf.py", "app:create"]