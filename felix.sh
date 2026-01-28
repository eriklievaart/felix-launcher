#!/bin/dash

die() {
	echo >&2 "$@"
	exit 1
}

shutdown() {
	echo "shutting down $pid"
	if [ "$pid" != "" -a "$pid" -gt 1000 ]; then
		kill -9 $pid
	fi
	exit 0
}

hot() {
	project="$1"
	project_home=~/Applications/$project
	[ -d "$project_home" ] || die "project does not exist: $project"
	[ -d data ] || mkdir data

	while true
	do
		echo "redeploying"
		java -client -classpath .:felix-launcher.jar:lib/*  com.eriklievaart.felix.boot.Main "$@" &
		pid=$!
		echo "pid=$pid"

		inotifywait -e create,modify "$project_home" "$project_home/bundle/"
		echo "application changes detected!"
		sleep 0.01

		echo "stopping process with pid $pid"
		kill $pid
		for i in $(seq 40)
		do
			[ ! -d "/proc/$pid" ] && break
			sleep 0.1
		done
		[ -d "/proc/$pid" ] && pkill -9 $pid

	done
}

cd ~/Applications/felix-launcher

if [ "$1" = "--hot" ]; then
	echo "\nfelix-launcher: hot redeploy enabled\n"
	shift
	trap 'shutdown' INT
	hot "$@"
elif [ "$1" = "--kill" ]; then
	pkill -f 'felix-launcher.jar'
else
	echo "\nfelix-launcher: running project '$1' without redeploy\n"
	java -client -classpath .:felix-launcher.jar:lib/*  com.eriklievaart.felix.boot.Main "$@"
fi

