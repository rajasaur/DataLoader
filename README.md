This allows you to upload Excel files, which are then converted into dynamic domain objects that can be edited using
a web interface rather than using an Excel file. Helps in improved communication between teams rather than passing
around Excel files.

### TODO
1. Improve the column formats (For e.g. Currencies/Dates etc so they can be edited accordingly). Right now everything is
a String, which may not help much
2. Load Data from Excel (Optional)
3. Use table as template for future versions, say, Inventory_V1
4. Multiple sheets, each sheet as separate table (select which ones to import as table -- format: xlsname_sheetname)
5. Update to Grails 2.1.0. The DynamicDomainService dint work with 2.1(, or maybe it was the demo), but this is the next
priority to do
6. Need to make a webapp for use directly (instead of doing run-app in dev mode).


### Issues
There maybe issues with this as Im testing it right now. Please let me know if there are any issues.

#### Credits

Credits go to the authors of the following:

* Dynamic Domain Service (http://code.google.com/p/grails-dynamic-domain-class-plugin/)
* http://www.technipelago.se/content/technipelago/blog/44 (Provided the idea for ExcelBuilder)
* Groovy / Grails Team
* Grails File Upload Plugin
