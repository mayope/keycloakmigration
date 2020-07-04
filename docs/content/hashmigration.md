---
title: Troubleshooting migration Hashes
type: docs
---
# Troubleshooting migration hashes

The hash implementation from 0.0.12 to 0.1.0 has changed so the old hashes will always throw an error.

An hash error may also occur if you did make a syntactic but not semantic change to the changelog.

You can however call the migration script with the `--correct-hashes` switch and it will just replace the failing hashes.
Alternatively you can execute the `KeycloakMigrationCorrectHashesTask` in gradle.

This will skip any control mechanism and you must to make sure that you have the same changelog that you migrated before.
It will only check for the number of hashes to skip or execute migrations!

> **Dont use the `--correct-hashes` switch in build pipelines!**


