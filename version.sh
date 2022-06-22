#!/bin/bash
set -e

git-version() {
  if [ -d .git ]; then
    git describe --always --tags --long
  else
    hg log -r . -T "{latesttag}-{latesttagdistance}-m{node|short}" --pager off
  fi
}

simple-version() {
  if [ -d .git ]; then
    echo "$(git rev-list --all --count).$(git rev-parse --short HEAD)"
  else
    hg log -r . -T "{rev}.{node|short}" --pager off
  fi

}

case "${1:-}" in
  git)
    long-version ;;
  simple)
    simple-version ;;
  *)
    git-version ;;
esac
