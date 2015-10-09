/*
Copyright © 2015 NoTalentHack, LLC

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.notalenthack.blaster;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CommandListAdapter extends BaseAdapter {

    private static final String TAG = CommandListAdapter.class.getCanonicalName();

	private ArrayList<Command> mCommands;
    private HashMap<String, Command> mMap;
	private LayoutInflater mInflater;
    private Activity mActivity;

	public CommandListAdapter(Activity par) {
		super();
        mActivity = par;
		mCommands  = new ArrayList<Command>();
        mMap = new HashMap<String, Command>();
		mInflater = par.getLayoutInflater();
	}
	
	public void addCommand(Command command) {
        mMap.put(command.getCommandStart(), command);   // allow for duplicate, we don't check
        mCommands.add(command);
	}

    public void addCommands(Set<Command> commands) {
        for (Command command : commands) {
            addCommand(command);
        }
    }

    public Set<String> getCommands() {
        Set<String> commands = new HashSet<String>();
        for (Command cmd : mCommands) {
            try {
                commands.add(cmd.toJSON().toString());
            } catch (JSONException ex) {
                Log.e(TAG, "Error parsing JSON " + cmd.getName());
            }
        }

        return commands;
    }
	
	public Command getCommand(int index) {
		return mCommands.get(index);
	}

    public void clearList() {
        mCommands.clear();
        mMap.clear();
    }

    public void updateStatus(int pos, Command.Status status) {
        Command cmd = mCommands.get(pos);
        cmd.setStatus(status);
    }

    public void updateCpuUsage(int pos, int cpu) {
        Command cmd = mCommands.get(pos);
        cmd.setCpuUsage(cpu);
    }

    @Override
	public int getCount() {
		return mCommands.size();
	}

	@Override
	public Object getItem(int position) {
		return getCommand(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// get already available view or create new if necessary
		FieldReferences fields;
        if (convertView == null) {
        	convertView = mInflater.inflate(R.layout.command_item, null);
        	fields = new FieldReferences();
            fields.imageView = (ImageView)convertView.findViewById(R.id.imageViewItem);
            fields.commandStart = (TextView)convertView.findViewById(R.id.commandLine);
        	fields.commandName = (TextView)convertView.findViewById(R.id.commandName);
            fields.cpuUsage = (TextView)convertView.findViewById(R.id.cpuUsage);

            convertView.setTag(fields);
        } else {
            fields = (FieldReferences) convertView.getTag();
        }			
		
        // set proper values into the view
        Command command = mCommands.get(position);
        String name = command.getName();
        String cmdStart = command.getCommandStart();
        int cpuUsage = command.getCpuUsage();

        if(name == null || name.length() <= 0) name = "Unknown";

        fields.imageView.setImageResource(command.getResourceId());
        fields.commandName.setText(name);
        fields.commandStart.setText(cmdStart);
        if (command.getDisplayStatus()) {
            StringBuffer buffer = new StringBuffer("Cpu ");
            buffer.append(Integer.toString(cpuUsage));
            buffer.append("%");
            fields.cpuUsage.setText(buffer.toString());
        } else {
            fields.cpuUsage.setText("");
        }

		return convertView;
	}
	
	private class FieldReferences {
        ImageView imageView;
        TextView commandName;
		TextView commandStart;
        TextView cpuUsage;
	}
}
