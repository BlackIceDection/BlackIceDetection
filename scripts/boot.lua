--[[
	Startup script called by 'Main.java'. Performs initial variable setup.
	This is just for the Java backend, though - Influx and MQTT stuff gets handled in the other scripts.
]]
print "+------------------+"
print "|     boot.lua     |"
print "+------------------+"





-- Whether to show the debugging window
--local show_debug_window = true

-- Lua scripts to execute right after this script
local other_scripts = {
	"db.lua",
	"mqtt.lua"
}





-- Check if it was the first run
if not main.firstRun then
	main:reset() -- If so, call reset() in all classes beforehand
end

--  SETTERS
main.debugWindow = show_debug_window
main:setChainScripts(other_scripts)

-- Since this script already ran, change firstRun to false
main.firstRun = false
