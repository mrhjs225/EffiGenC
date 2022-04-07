import os

os.system('mvn package')
os.system('mv target/TBar-0.0.1-SNAPSHOT.jar target/dependency/TBar-0.0.1-SNAPSHOT.jar')
os.system('export JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8')

# projects = ['Chart', 'Closure', 'Lang', 'Math', 'Mockito', 'Time']
projects = []
bugList = []
mode = 'project'

# 1: 148.effigenc1, 2: 148.effigenc2, 3:160.effigenc1, 4:160.effigenc2
machine_num = 1

if machine_num == 1:
    projects = ['Chart', 'Closure']
elif machine_num == 3:
    projects = ['Closure', 'Lang']
elif machine_num == 4:
    projects = ['Math']
elif machine_num == 2:
    projects = ['Mockito', 'Time']

for project_name in projects:
    buggy_num = 0
    start_num = 1

    if project_name == 'Chart':
        buggy_num = 26
    elif project_name == 'Closure':
        if machine_num == 1:
            buggy_num = 80
        elif machine_num == 3:
            start_num = 81
            buggy_num = 133
    elif project_name == 'Lang':
        buggy_num = 65
    elif project_name == 'Math':
        buggy_num = 106
    elif project_name == 'Mockito':
        buggy_num = 38
    elif project_name == 'Time':
        buggy_num = 27

    for temp_buggy_num in range(start_num, buggy_num+1):
        if project_name == 'Cli' and temp_buggy_num == 6:
            continue
        elif project_name == 'Closure' and (temp_buggy_num == 63 or temp_buggy_num == 93):
            continue
        elif project_name == 'Collections' and (temp_buggy_num <= 24):
            continue
        elif project_name == 'Lang' and temp_buggy_num == 2:
            continue
        elif project_name == 'Time' and temp_buggy_num == 21:
            continue
        bugId = project_name + '_' + str(temp_buggy_num)
        os.system('./PerfectFLTBarRunner.sh /root/projects/ ' + bugId + ' /root/opt/defects4j/ false ' + mode)