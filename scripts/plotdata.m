#################################################################################### 
# This script plots learning curves of SimpleDS agents 
# <ahref="mailto:h.cuayahuitl@gmail.com">Heriberto Cuayahuitl</a>
####################################################################################

arg_list = argv ();
if (nargin < 1) 
  printf ("Usage: sampleplot.m InputDataFile.out [OutputFile.png]");
  exit
else 
  inputfile = arg_list{1};
endif

fid=fopen(inputfile); 
line=0;
data=zeros(3,6);
line_i=1; 
while (-1 ~= (line=fgetl(fid))) 
  data(line_i++,:)=str2num(line); 
end 
fclose(fid);  

x = data(:,1); 
y = data(:,2);
yy = data(:,6);
[h,h1,h2] = plotyy(x, y, x, yy);
set(h, "fontsize", 12, "linewidth", 2.0);
set(h1, "LineStyle", "-", "linewidth", 2.0);
set(h2, "LineStyle", "--", "linewidth", 2.0);
grid on;
xlabel('Learning Steps (no. experiences)');
ylabel(h(1), 'Average Reward');
ylabel(h(2), 'Learning Time (in hours)');

pause

if (nargin == 2)
  outputfile = arg_list{2};
  saveas(1, outputfile);
endif
