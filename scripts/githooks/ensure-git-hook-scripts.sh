#!/usr/bin/env bash

# Copyright (C) 2023 dowenliu-xyz
#
#  This program is free software: you can redistribute it and/or modify
#  it under the terms of the GNU Affero General Public License as published
#  by the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU Affero General Public License for more details.
#
#  You should have received a copy of the GNU Affero General Public License
#  along with this program.  If not, see <https://www.gnu.org/licenses/>.

if [ ! -f .git/hooks/commit-msg ]; then
  echo "cp scripts/githooks/commit-msg .git/hooks/commit-msg"
  cp scripts/githooks/commit-msg .git/hooks/commit-msg
fi

if ! cmp -s scripts/githooks/commit-msg .git/hooks/commit-msg; then
  echo "cp scripts/githooks/commit-msg .git/hooks/commit-msg"
  cp scripts/githooks/commit-msg .git/hooks/commit-msg
fi

if ! [[ -x .git/hooks/commit-msg ]]; then
  echo "chmod +x .git/hooks/commit-msg"
  chmod +x .git/hooks/commit-msg
fi

if [ ! -f .git/hooks/pre-commit ]; then
  echo "cp scripts/githooks/pre-commit .git/hooks/pre-commit"
  cp scripts/githooks/pre-commit .git/hooks/pre-commit
fi

if ! cmp -s scripts/githooks/pre-commit .git/hooks/pre-commit; then
  echo "cp scripts/githooks/pre-commit .git/hooks/pre-commit"
  cp scripts/githooks/pre-commit .git/hooks/pre-commit
fi

if ! [[ -x .git/hooks/pre-commit ]]; then
  echo "chmod +x .git/hooks/pre-commit"
  chmod +x .git/hooks/pre-commit
fi
