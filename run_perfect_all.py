import os

os.system('mvn package')
os.system('mv target/TBar-0.0.1-SNAPSHOT.jar target/dependency/TBar-0.0.1-SNAPSHOT.jar')
os.system('export JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8')

projects = ['Chart', 'Closure', 'Lang', 'Math', 'Mockito', 'Time']
projects = ['Chart']
bugList = []
mode = 'project'

for project_name in projects:
    buggy_num = 0
    start_num = 1
    # start_num = 7

    if project_name == 'Chart':
        buggy_num = 26
        buggy_num = 11
    elif project_name == 'Closure':
        start_num = 62
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
        bugId = project_name + '_' + str(temp_buggy_num)
        os.system('./PerfectFLTBarRunner.sh /root/projects/ ' + bugId + ' /root/opt/defects4j/ false ' + mode)
        # os.system('./PerfectFLTBarRunner.sh /root/projects/ ' + bugId + ' /root/opt/defects4j/ false ' + mode + ' > /root/EffiGenC/Results/IngredientRank/' + project_name + '_' + str(temp_buggy_num) + '_' + mode + '.txt')