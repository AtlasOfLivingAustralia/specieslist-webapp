-- This is ran on the BHL database using the SQLServer console tools
-- with a report to File option (see the context menu - right click - in the SQL query pane)

select t.TitleID, t.FullTitle, t.ShortTitle, i.ItemID, p.PageID, pn.NameConfirmed, pn.NameFound
from BHL.dbo.Item i
inner join BHL.dbo.Title t ON i.PrimaryTitleID = t.TitleID
inner join BHL.dbo.Page p ON p.ItemID = i.ItemID
inner join BHL.dbo.PageName pn  ON p.PageID = pn.PageID;