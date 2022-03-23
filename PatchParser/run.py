import os

# project = ['Chart', 'Closure', 'Lang', 'Math', 'Mockito', 'Time']
project = ['Chart', 'Closure', 'Lang', 'Math', 'Mockito', 'Time', \
            'Cli', 'Codec', 'Collections', 'Compress', 'Csv', 'Gson',\
            'JacksonCore', 'JacksonDatabind', 'JacksonXml', 'Jsoup', 'JxPath']

# project = ['Chart']
basic_dir = '/root/EffiGenC/Answerfiles/'

os.chdir('Parser')
os.system('mvn compile')
os.system('mvn dependency:copy-dependencies')
os.system('mvn package')
os.system('mv -f target/Parser-0.0.1-SNAPSHOT.jar target/dependency/')

for project_name in project:
    buggy_num = 0
    start_num = 1
    # start_num = 15
    if project_name == 'Chart':
        buggy_num = 26
        # buggy_num = 2
    elif project_name == 'Cli':
        buggy_num = 40
        # buggy_num = 1
    elif project_name == 'Closure':
        #start_num = 175
        buggy_num = 176
    elif project_name == 'Codec':
        buggy_num = 18
    elif project_name == 'Collections':
        buggy_num = 28
    elif project_name == 'Compress':
        buggy_num = 47
    elif project_name == 'Csv':
        buggy_num = 16
    elif project_name == 'Gson':
        buggy_num = 18
    elif project_name == 'JacksonCore':
        buggy_num = 26
    elif project_name == 'JacksonDatabind':
        buggy_num = 112
    elif project_name == 'JacksonXml':
        buggy_num = 6
    elif project_name == 'Jsoup':
        buggy_num = 93
    elif project_name == 'JxPath':
        buggy_num = 22
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
        target_dir = basic_dir + project_name + '/' + str(temp_buggy_num)
        if not os.path.exists(target_dir):
            os.makedirs(target_dir)
        target_dir += '/changeAction.txt'
        # target_dir += '/changeActionNodeType.txt'
        # os.system('java -cp \"target/dependency/*\" -Xmx2g edu.lu.uni.serval.Main3 ' + project_name + ' ' + str(temp_buggy_num) + ' > ' + target_dir)
        # print('{} {}'.format(project_name, str(temp_buggy_num)))
        os.system('java -cp \"target/dependency/*\" -Xmx2g edu.lu.uni.serval.Main3 ' + project_name + ' ' + str(temp_buggy_num))
        # print('{} {} done'.format(project_name, str(temp_buggy_num)))
