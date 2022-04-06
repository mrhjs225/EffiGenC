import os

os.system('mvn package')
os.system('mv target/TBar-0.0.1-SNAPSHOT.jar target/dependency/TBar-0.0.1-SNAPSHOT.jar')
os.system('export JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8')

projects = ['Chart', 'Closure', 'Lang', 'Math', 'Mockito', 'Time']
projects = ['Chart']
bugList = []
mode = 'project'

for project_name in projects:
    if project_name == 'Chart':
        bugList = [1, 4, 7, 8, 9, 11, 12, 14, 18, 19, 20, 24, 25, 26]
        bugList = [5]
    elif project_name == 'Closure':
        bugList = [2, 4, 6, 10, 11, 13, 18, 21, 22, 31, 38, 40, 46, 62, 63, 70, 73, 85, 86, 102, 106, 115, 126]
        bugList = [62]
    elif project_name == 'Lang':
        bugList = [6, 7, 10, 15, 22, 24, 26, 33, 39, 47, 51, 57, 59, 63]
    elif project_name == 'Math':
        bugList = [4, 5, 11, 15, 22, 30, 33, 34, 35, 50, 57, 58, 59, 65, 70, 75, 77, 79, 80, 82, 85, 89, 98]
    elif project_name == 'Mockito':
        bugList = [26, 29, 38]
    elif project_name == 'Time':
        bugList = [3, 7, 19, 26]
    
    for bugNum in bugList:
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
        bugId = project_name + '_' + str(bugNum)
        # os.system('./PerfectFLTBarRunner.sh /root/projects/ ' + bugId + ' /root/opt/defects4j/ false > ./Results/' + bugId + '.txt')
        os.system('./PerfectFLTBarRunner.sh /root/projects/ ' + bugId + ' /root/opt/defects4j/ false ' + mode)