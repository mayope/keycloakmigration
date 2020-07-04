---
title: Implementation Substitution
type: docs
---
## Implementation Details

The migration hashes are stored in the attribute named 'migration' in the migration user.

There are no transactions in keycloak though if the rollback fails there might be a non deterministic state!
If it fails I would like to receive a bug report for this.

If you struggle with invalid hashes on a linux-windows setup check the line endings of the json-import files as git might check them out as LF where on windows its CRLF and thus producing the error in hashing.

If you are using git you can place the following file into the dir with the json-import-files to reassure that the line ending is always LF.

`.gitattributes`:

    ** text eol=lf
