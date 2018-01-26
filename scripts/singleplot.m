#################################################################################### 
# This script plots learning curves of SimpleDS agents 
# <ahref="mailto:h.cuayahuitl@gmail.com">Heriberto Cuayahuitl</a>
####################################################################################

graphics_toolkit gnuplot

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

figure
x = data(:,1); 
y = data(:,5);
yy = data(:,6);

hold on; 
[h,h1,h2] = plotyy(x, y, x, yy);

set(h, "fontsize", 12, "linewidth", 2.0);
set(h1, "LineStyle", "-", "linewidth", 2.0);
set(h2, "LineStyle", "--", "linewidth", 2.0);
set(h(1),'ycolor','red');
set(h1, "color", "r");
grid on;
xlabel('Learning Steps (no. experiences)');
ylabel(h(1), 'Classification Accuracy');
ylabel(h(2), 'Learning Time (in hours)');

pause

if (nargin == 2)
  outputfile = arg_list{2};
  saveas(1, outputfile);
endif
