#!/bin/dash

die() {
	echo >&2 "$@"
	exit 1
}

snapshot() {
	ls -l "$project_home" "$project_home/bundle" > data/$project-$1.txt
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
		snapshot running
		echo "redeploying"
		java -client -classpath .:@project@.jar:lib/* @jvm.opts@ @main.class@ "$@" &
		pid=$!

		while true
		do
			sleep 0.1
			snapshot current
			if [ "$(diff data/$project-running.txt data/$project-current.txt)" != "" ]; then
				echo "changes detected for project '$project'"
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

cd ~/Applications/@project@

if [ "$1" = "--hot" ]; then
	echo "\nfelix-launcher: hot redeploy enabled\n"
	shift
	trap 'shutdown' INT
	hot "$@"
elif [ "$1" = "--kill" ]; then
	pkill -f 'felix-launcher.jar'
else
	echo "\nfelix-launcher: running project '$1' without redeploy\n"
	java -client -classpath .:@project@.jar:lib/* @jvm.opts@ @main.class@ "$@"
fi

