#!/bin/sh
set -e

# Handle TOTP
TOTP=""
[ -n "${ADMIN_TOTP}" ] && TOTP="-t ${ADMIN_TOTP}"

# Base arguments
ARGUMENTS="${KEYCLOAK_CHANGELOG:-/migration/keycloak-changelog.yml}"
ARGUMENTS="$ARGUMENTS -u ${ADMIN_USER:-admin} -p ${ADMIN_PASSWORD:-admin} $TOTP"
ARGUMENTS="$ARGUMENTS -c ${ADMIN_CLIENT:-admin-cli} -r ${LOGIN_REALM:-master}"
ARGUMENTS="$ARGUMENTS -b ${BASEURL:-http://localhost:8080/auth}"
ARGUMENTS="$ARGUMENTS --wait-for-keycloak-timeout ${WAIT_FOR_KEYCLOAK_TIMEOUT:-0}"

# Optional flags
[ -n "${WAIT_FOR_KEYCLOAK+x}" ] && ARGUMENTS="$ARGUMENTS --wait-for-keycloak" || echo "dont wait for keycloak"
[ -n "${LOGIN_WITH_CLIENT_SECRET+x}" ] && ARGUMENTS="$ARGUMENTS --login-with-client-credentials" || echo "don't login with client secret"
[ -n "${CLIENT_SECRET+x}" ] && ARGUMENTS="$ARGUMENTS --client-secret ${CLIENT_SECRET}" || echo "don't use client secret"
[ -n "${FAIL_ON_UNDEFINED_VARIABLES+x}" ] && ARGUMENTS="$ARGUMENTS --fail-on-undefined-variables" || echo "dont fail on errors"
[ -n "${DISABLE_WARN_ON_UNDEFINED_VARIABLES+x}" ] && ARGUMENTS="$ARGUMENTS --disable-warn-on-undefined-variables" || echo "warn on errors"
[ -n "${CORRECT_HASHES+x}" ] && ARGUMENTS="$ARGUMENTS --correct-hashes" || echo "dont correct hashes"

# Print if requested
[ -n "${PRINT_ARGUMENTS}" ] && echo "$ARGUMENTS"

# Run migration
set +e
java -jar keycloakmigration.jar $ARGUMENTS
RESULT=$?
set -e

# Handle exit behavior
if [ $RESULT -ne 0 ]; then
  echo "Migration failed with exit code $RESULT"
  if [ -n "${EXIT_ON_ERROR+x}" ]; then
    echo "EXIT_ON_ERROR is set — exiting..."
    exit $RESULT
  else
    echo "EXIT_ON_ERROR not set — continuing..."
  fi
else
  echo "Migration completed successfully."
fi

# Stay idle if requested
if [ -n "${STAY_IDLE+x}" ]; then
  echo "Staying idle..."
  tail -f /dev/null
fi
