import os

os.system('mvn package')
os.system('mv target/TBar-0.0.1-SNAPSHOT.jar target/dependency/TBar-0.0.1-SNAPSHOT.jar')
os.system('export JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8')

projects = ['Chart', 'Closure', 'Lang', 'Math', 'Mockito', 'Time']
projects = ['Chart']
bugList = []
# method, file, package, project
modes = ['method', 'file', 'package', 'project']
modes = ['project']

for project in projects:
    if project == 'Chart':
        bugList = [1, 4, 7, 8, 9, 11, 12, 14, 18, 19, 20, 24, 25, 26]
        bugList = [7]
    elif project == 'Closure':
        bugList = [2, 4, 6, 10, 11, 13, 18, 21, 22, 31, 38, 40, 46, 62, 63, 70, 73, 85, 86, 102, 106, 115, 126]
    elif project == 'Lang':
        bugList = [6, 7, 10, 15, 22, 24, 26, 33, 39, 47, 51, 57, 59, 63]
    elif project == 'Math':
        bugList = [4, 5, 11, 15, 22, 30, 33, 34, 35, 50, 57, 58, 59, 65, 70, 75, 77, 79, 80, 82, 85, 89, 98]
    elif project == 'Mockito':
        bugList = [26, 29, 38]
    elif project == 'Time':
        bugList = [3, 7, 19, 26]
    
    for bugNum in bugList:
        for mode in modes:
            bugId = project + '_' + str(bugNum)
            # os.system('./PerfectFLTBarRunner.sh /root/projects/ ' + bugId + ' /root/opt/defects4j/ false > ./Results/' + bugId + '.txt')
            os.system('./PerfectFLTBarRunner.sh /root/projects/ ' + bugId + ' /root/opt/defects4j/ false ' + mode)