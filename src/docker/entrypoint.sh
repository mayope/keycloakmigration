#!/bin/sh
if [ -z ${ADMIN_TOTP} ]; then TOTP=""; else TOTP="-t $ADMIN_TOTP"; fi
ARGUMENTS="'${KEYCLOAK_CHANGELOG:-/migration/keycloak-changelog.yml}' -u '${ADMIN_USER:-admin}' -p '${ADMIN_PASSWORD:-admin}' $TOTP -c '${ADMIN_CLIENT:-admin-cli}' -r '${LOGIN_REALM:-master}' -b '${BASEURL:-http://localhost:8080/auth}' --wait-for-keycloak-timeout ${WAIT_FOR_KEYCLOAK_TIMEOUT:-0}"

if [ -z ${WAIT_FOR_KEYCLOAK+x} ]; then echo "dont wait for keycloak"; else ARGUMENTS="$ARGUMENTS --wait-for-keycloak"; fi
if [ -z ${LOGIN_WITH_CLIENT_SECRET+x} ]; then echo "don't login with client secret"; else ARGUMENTS="$ARGUMENTS --login-with-client-credentials"; fi
if [ -z ${CLIENT_SECRET+x} ]; then echo "don't use client secret"; else ARGUMENTS="$ARGUMENTS --client-secret '${CLIENT_SECRET}'"; fi
if [ -z ${FAIL_ON_UNDEFINED_VARIABLES+x} ]; then echo "dont fail on errors"; else ARGUMENTS="$ARGUMENTS --fail-on-undefined-variables"; fi
if [ -z ${DISABLE_WARN_ON_UNDEFINED_VARIABLES+x} ]; then echo "warn on errors"; else ARGUMENTS="$ARGUMENTS --disable-warn-on-undefined-variables"; fi
if [ -z ${CORRECT_HASHES+x} ]; then echo "dont correct hashes"; else ARGUMENTS="$ARGUMENTS --correct-hashes"; fi
if [ -z ${STAY_IDLE+x} ]; then echo "dont stay idle"; else ARGUMENTS="$ARGUMENTS || true && tail -f /dev/null "; fi

if [ -z ${PRINT_ARGUMENTS} ]; then echo ""; else echo "$ARGUMENTS"; fi
/bin/sh -c "java -jar keycloakmigration.jar $ARGUMENTS" || true
