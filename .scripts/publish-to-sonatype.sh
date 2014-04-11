#!/bin/bash

if [ "$TRAVIS_REPO_SLUG" == "testinfected/molecule" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ]; then
  if [[ $(gradle -q version) != *SNAPSHOT* ]]; then
    echo 'Travis will only publish snapshots. instance.'
    exit 0
  fi

  echo -e "Publishing to Sonatype OSS Maven Repository...\n"

  gradle uploadArchives -PnexusUsername="${SONATYPE_USERNAME}" -PnexusPassword="${SONATYPE_PASSWORD}"

  RETVAL=$?

  if [ $RETVAL -eq 0 ]; then
      echo -e '\nPublished!'
      exit 0
  else
      echo -e '\nPublish failed.'
      exit 1
  fi
fi