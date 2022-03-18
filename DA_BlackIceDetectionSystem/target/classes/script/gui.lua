--[[
	Sets up debugging window.
	Goes unused for the final build.
]]





-- Window title
frame:setTitle("Black Ice Detection System - Debug")
-- Window dimensions
frame:setSize(1600, 600)
-- Center window?
frame:setLocationRelativeTo(nil)



-- Layout and the rest of the GUI behavior/components are hardcoded in Java because why make the trouble of dynamically
-- assembling a GUI when it's not even going to be used in the final product anyways.
