import os
from tqdm import tqdm

os.system('mvn package')
os.system('mv target/TBar-0.0.1-SNAPSHOT.jar target/dependency/TBar-0.0.1-SNAPSHOT.jar')
os.system('export JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8')

projects = ['Chart', 'Closure', 'Lang', 'Math', 'Mockito', 'Time']
bugList = []
mode = 'File' # Method, File, Package, Project

# 1: 148.direction1, 2: 148.direction2, 3:kkc_direction1, 4:kkc_direction2, 5: soty_direction_1, 6:soty_direction_2
machine_num = 1

if machine_num == 1:
    mode = 'Method'
elif machine_num == 2:
    mode = 'File'
elif machine_num == 3:
    projects = ['Chart', 'Closure', 'Lang']
    mode = 'Package'
elif machine_num == 4:
    projects = ['Math', 'Mockito', 'Time']
    mode = 'Package'
elif machine_num == 5:
    projects = ['Chart', 'Closure', 'Lang']
    mode = 'Project'
elif machine_num == 6:
    projects = ['Math', 'Mockito', 'Time']
    mode = 'Project'

for project_name in projects:
    buggy_num = 0
    start_num = 1

    if project_name == 'Chart':
        buggy_num = 26
    elif project_name == 'Closure':
        buggy_num = 133
    elif project_name == 'Lang':
        buggy_num = 65
    elif project_name == 'Math':
        buggy_num = 106
    elif project_name == 'Mockito':
        buggy_num = 38
    elif project_name == 'Time':
        buggy_num = 27

    for temp_buggy_num in tqdm(range(start_num, buggy_num+1)):
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
        os.system('./PerfectFLTBarRunner.sh /root/projects/ ' + bugId + ' /root/opt/defects4j/ false ' + mode + ' > ./Data/Results/TerminalOutput/' + bugId + '.txt')
