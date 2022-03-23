import os

project = ['Chart', 'Closure', 'Lang', 'Math', 'Mockito', 'Time', \
            'Cli', 'Codec', 'Collections', 'Compress', 'Csv', 'Gson',\
            'JacksonCore', 'JacksonDatabind', 'JacksonXml', 'Jsoup', 'JxPath']
project = ['Chart']
os.system('export JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8')

for project_name in project:
    buggy_num = 0
    start_num = 1
    start_num = 7

    if project_name == 'Chart':
        buggy_num = 26
        buggy_num = 7
    elif project_name == 'Cli':
        buggy_num = 40
        # buggy_num = 1
    elif project_name == 'Closure':
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
        basic_dir = '/root/EffiGenC/Answerfiles/' + project_name + '/' + str(temp_buggy_num) + '/'
        before_file_dir = basic_dir + 'before/'
        after_file_dir = basic_dir + 'after/'
        diff_file_dir = '/root/EffiGenC/Answerfiles/diffs/' + project_name + '_' + str(temp_buggy_num) + '.diff'

        answer_file_list = []
        try:
            f = open(diff_file_dir, 'r')
            while True:
                line = f.readline()
                if not line: break
                if (line.startswith('---')):
                    answer_file_list.append(line.split('--- ')[1].strip())
            f.close()
        except:
            continue
        
        for (root, directories, files) in os.walk(before_file_dir):
            for file in files:
                final_before_file_dir = before_file_dir + file
                final_after_file_dir = after_file_dir + file
        # os.system('java -cp "target/*" gumtree.spoon.AstComparator ' + final_before_file_dir + ' ' + final_after_file_dir + ' > ' + basic_dir + 'spoon_changeAction.txt')
        os.system('java -cp "target/*" gumtree.spoon.AstComparator ' + final_before_file_dir + ' ' + final_after_file_dir)