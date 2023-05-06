FROM ubuntu:kinetic

ENV DB_CONNECTION_STRING=???LOL???

WORKDIR /usr/app

RUN apt-get update -y
RUN apt-get install build-essential -y
RUN apt-get install libpq-dev -y
RUN apt-get install python3.11-dev -y
RUN apt-get install curl -y
RUN curl -sS https://bootstrap.pypa.io/get-pip.py | python3.11

COPY requirements.txt .
RUN pip install -r requirements.txt

COPY app_conf.py .
COPY app.py .

CMD ["hypercorn", "--config", "file:/usr/app/app_conf.py", "app:create"]