FROM python:3.9-alpine

WORKDIR /app

COPY . .

RUN apk update && apk add postgresql-dev gcc python3-dev musl-dev
RUN pip install -r requirements.txt

ENV PYTHONUNBUFFERED=1

EXPOSE 8000

CMD ["./gunicorn_start.sh"]
