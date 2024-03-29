use UserManagement
go

set nocount on

declare
  @rc int,
  @context xml,
  @errorinfo nvarchar(4000);
  
select
  @context = '<contextparams><contextparam key="LCID">1033</contextparam><contextparam key="default_LCID">1033</contextparam><contextparam key="UserID">1</contextparam></contextparams>';
--select
--  @context = null;
 
delete
  from dbo.MenuPermission;  
delete
  from dbo.Menu;

-- DO NOT FORGET TO UPDATE THIS !!!!!!!!!!!!!!!!!
-- Next NUMBER is 4
--

-- Security
exec @rc = dbo.P_Menu_I @context, 1, null,  1, 
  null, 
  N'<messages xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema"><message lcid="1033">Security</message></messages>', @errorinfo OUTPUT
if (@rc <> 0) goto error;  
exec @rc = dbo.P_Menu_I @context, 2,    1,  1, 
  N'~/Roles.aspx', 
  N'<messages xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema"><message lcid="1033">Roles</message></messages>', @errorinfo OUTPUT
if (@rc <> 0) goto error;  
exec @rc = dbo.P_Menu_I @context, 3,    1,  2, 
  N'~/Users.aspx', 
  N'<messages xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema"><message lcid="1033">Users</message></messages>', @errorinfo OUTPUT
if (@rc <> 0) goto error;  

--
-- Permissions
--

-- Users
exec @rc = dbo.P_MenuPermission_I @context, 3, 133, @errorinfo OUTPUT;
if (@rc <> 0) goto error;
-- Roles
exec @rc = dbo.P_MenuPermission_I @context, 2, 125, @errorinfo OUTPUT;
if (@rc <> 0) goto error;

goto no_errors
error:
if (@rc <> 0)
begin
  select @rc as "Error code", @errorinfo as "Error Information"
  raiserror(60002, 16, 1);
end  
no_errors:
