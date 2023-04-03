FROM ruby:3.2.2

WORKDIR /power
ENV RAILS_ENV="production"

RUN gem update --system --no-document && gem install -N bundler

RUN apt-get update -qq && apt-get install --no-install-recommends -y build-essential pkg-config

COPY . /power/

RUN bundle install && bundle exec bootsnap precompile --gemfile && bundle exec bootsnap precompile app/ lib/

EXPOSE 8080
CMD ["/power/bin/rails", "server"]
