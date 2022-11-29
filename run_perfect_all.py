import os
from tqdm import tqdm

os.system('mvn package')
os.system('mv target/TBar-0.0.1-SNAPSHOT.jar target/dependency/TBar-0.0.1-SNAPSHOT.jar')
os.system('export JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8')

projects = ['Chart', 'Closure', 'Lang', 'Math', 'Mockito', 'Time']
bugList = []
mode = 'File' # Method, File, Package, Project

# apr1: 148.direction1, apr2: 148.direction2, apr3: 148.direction3, apr4: 148.direction4
# kkc1:kkc_direction1, kkc2:kkc_direction2, kkc3:kkc_direction3, kkc4:kkc_direction4
# soty1: soty_direction1, soty2:soty_direction2, soty3:soty_direction3, soty4:soty_direction4, soty5:soty_direction5, soty6:soty_direction6, soty7:soty_direction7, soty8:soty_direction8
machine_id = 'apr1'

if machine_id == 'apr1':
    mode = 'Method'
    projects = ['Chart', 'Closure', 'Lang']
elif machine_id == 'apr2':
    mode = 'Method'
    projects = ['Math', 'Mockito', 'Time']
elif machine_id == 'apr3':
    mode = 'File'
    projects = ['Chart', 'Closure', 'Lang']
elif machine_id == 'apr4':
    mode = 'File'
    projects = ['Math', 'Mockito', 'Time']
elif machine_id == 'kkc1':
    mode = 'Package'
    projects = ['Chart', 'Lang']
elif machine_id == 'kkc2':
    mode = 'Package'
    projects = ['Closure']
elif machine_id == 'kkc3':
    mode = 'Package'
    projects = ['Math']
elif machine_id == 'kkc4':
    mode = 'Package'
    projects = ['Mockito', 'Time']
elif machine_id == 'soty1':
    mode = 'Project'
    projects = ['Chart', 'Closure']
elif machine_id == 'soty2':
    mode = 'Project'
    projects = ['Closure']
elif machine_id == 'soty3':
    mode = 'Project'
    projects = ['Closure']
elif machine_id == 'soty4':
    mode = 'Project'
    projects = ['Lang']
elif machine_id == 'soty5':
    mode = 'Project'
    projects = ['Lang', 'Math']
elif machine_id == 'soty6':
    mode = 'Project'
    projects = ['Math']
elif machine_id == 'soty7':
    mode = 'Project'
    projects = ['Math']
elif machine_id == 'soty8':
    mode = 'Project'
    projects = ['Mockito', 'Time']

for project_name in projects:
    buggy_num = 0
    start_num = 1

    if project_name == 'Chart':
        buggy_num = 26
    elif project_name == 'Closure':
        buggy_num = 133
        if machine_id == 'soty1':
            buggy_num = 33
        elif machine_id == 'soty2':
            start_num = 34
            buggy_num = 83
        elif machine_id == 'soty3':
            start_num = 84
            buggy_num = 133
    elif project_name == 'Lang':
        buggy_num = 65
        if machine_id == 'soty4':
            buggy_num = 50
        elif machine_id == 'soty5':
            start_num = 51
            buggy_num = 65
    elif project_name == 'Math':
        buggy_num = 106
        if machine_id == 'soty5':
            buggy_num = 35
        elif machine_id == 'soty6':
            start_num = 36
            buggy_num = 85
        elif machine_id == 'soty7':
            start_num = 86
            buggy_num = 106
    elif project_name == 'Mockito':
        buggy_num = 38
    elif project_name == 'Time':
        buggy_num = 27

    for bug_num in tqdm(range(start_num, buggy_num+1)):
        if project_name == 'Cli' and bug_num == 6:
            continue
        elif project_name == 'Closure' and (bug_num == 63 or bug_num == 93):
            continue
        elif project_name == 'Collections' and (bug_num <= 24):
            continue
        elif project_name == 'Lang' and bug_num == 2:
            continue
        elif project_name == 'Time' and bug_num == 21:
            continue
        bugId = project_name + '_' + str(bug_num)
        os.system('./PerfectFLTBarRunner.sh /root/projects/ ' + bugId + ' /root/opt/defects4j/ false ' + mode + ' > ./Data/Results/TerminalOutput/' + bugId + '.txt')

os.system('cp /root/DIRECTION/Data/HitRatio/keyword_based_search.csv /root/dockermount/keyword_based_search_{}.csv'.format(machine_id))
os.system('cp /root/DIRECTION/Data/HitRatio/keyword_based_search_treeinfo.csv /root/dockermount/keyword_based_search_treeinfo_{}.csv'.format(machine_id))
