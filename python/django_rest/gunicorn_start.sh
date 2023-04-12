#!/bin/sh

NAME="django_rest"
DJANGODIR=/app
USER=root
GROUP=root
NUM_WORKERS=4
DJANGO_SETTINGS_MODULE=django_rest.settings
DJANGO_WSGI_MODULE=django_rest.wsgi

echo "Starting $NAME as `whoami`"

cd $DJANGODIR
export DJANGO_SETTINGS_MODULE=$DJANGO_SETTINGS_MODULE
export PYTHONPATH=$DJANGODIR:$PYTHONPATH

exec gunicorn ${DJANGO_WSGI_MODULE} \
    --name $NAME \
    --workers $NUM_WORKERS \
    --user=$USER --group=$GROUP \
    --bind=0.0.0.0:8000 \
    --log-level=debug \
    --log-file=-
