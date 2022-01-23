print "+------------------+"
print "|     boot.lua     |"
print "+------------------+"


-- show GUI?
local show_debug_window = true

-- Lua scripts that get executed right after this script
local other_scripts = {
	"db.lua",
	"mqtt.lua"
}



-- SETTER
if not main.firstRun then
	main:reset()
end

main.debugWindow = show_debug_window
main:setChainScripts(other_scripts)

main.firstRun = false
