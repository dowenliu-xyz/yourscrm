include .env
export

.PHONY: git.hooks
git.hooks: tools.require.go-gitlint
	@chmod +x ./scripts/githooks/ensure-git-hook-scripts.sh && ./scripts/githooks/ensure-git-hook-scripts.sh

.PHONY: tools.require.%
tools.require.%:
	@chmod +x ./scripts/tools/$*.sh && ./scripts/tools/$*.sh