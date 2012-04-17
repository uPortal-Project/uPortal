find uportal-war/src/ \( -name '*.java' -or -name '*.xml' \) | xargs grep -oh ${[^}]*} | sort -u
