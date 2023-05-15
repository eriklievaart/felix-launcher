#!/bin/dash

cd ~/Applications/@project@

shutdown() {
	echo "shutting down $pid"
	if [ "$pid" != "" -a "$pid" -gt 1000 ]; then
		kill -9 $pid
	fi
	exit 0
}

hot() {
	project="$1"
	[ -d data ] || mkdir data

	while true
	do
		ls -l ~/Applications/$project/bundle > data/$project-running.txt
		java -client -classpath .:@project@.jar:lib/* @jvm.opts@ @main.class@ "$@" &
		pid=$!

		while true
		do
			sleep 0.1
			ls -l ~/Applications/$project/bundle > data/$project-current.txt
			if [ "$(diff data/$project-running.txt data/$project-current.txt)" != "" ]; then
				echo "changes detected, redeploying '$project'"
				break
			fi
		done

		echo "stopping process with pid $pid"
		kill $pid
		for i in $(seq 40)
		do
			[ -d "/proc/pid" ] || break
			sleep 0.1
		done
		[ -d "/proc/pid" ] && pkill -9 $pid

	done
}

if [ "$1" = "--hot" ]; then
	echo "hot redeploy enabled"
	shift
	trap 'shutdown' INT
	hot "$@"
else
	java -client -classpath .:@project@.jar:lib/* @jvm.opts@ @main.class@ "$@"
fi

