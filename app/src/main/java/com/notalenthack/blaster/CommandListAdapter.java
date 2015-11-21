/*
Copyright © 2015-2016 Dzung Tran (dzungductran@yahoo.com)

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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CommandListAdapter extends BaseAdapter  {

    private static final String TAG = CommandListAdapter.class.getCanonicalName();

	private ArrayList<Command> mCommands;
	private LayoutInflater mInflater;
    private CommandActivity mActivity;

    public CommandListAdapter(CommandActivity par) {
		super();
        mActivity = par;
		mCommands  = new ArrayList<Command>();
		mInflater = par.getLayoutInflater();
	}
	
	public void addCommand(Command command) {
        mCommands.add(command);
	}

    public void addCommand(int index, Command command) {
        mCommands.add(index, command);
    }

    public void addCommands(Set<Command> commands) {
        for (Command command : commands) {
            addCommand(command);
        }
    }

    public void deleteCommand(Command command) {
        mCommands.remove(command);
    }


    public JSONArray getCommandsAsJSONArray() {
        JSONArray jsonArray = new JSONArray();
        for (Command cmd : mCommands) {
            try {
                jsonArray.put(cmd.toJSON());
            } catch (JSONException ex) {
                Log.e(TAG, "Error parsing JSON " + cmd.getName());
                jsonArray = new JSONArray(); // empty json array
                return jsonArray;
            }
        }

        return jsonArray;
    }

    public List<Command> getCommands() { return mCommands; }

	public Command getCommand(int index) {
		return mCommands.get(index);
	}

    public void clearList() {
        mCommands.clear();
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
            fields.commandStart = (TextView)convertView.findViewById(R.id.commandLine);
        	fields.commandName = (TextView)convertView.findViewById(R.id.commandName);
            fields.cpuUsage = (TextView)convertView.findViewById(R.id.cpuUsage);
            fields.playButton = (ImageButton)convertView.findViewById(R.id.btnCommandAction);
            fields.imageView = (ImageView)convertView.findViewById(R.id.btnEditCommand);

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

        // only allow edit & delete on non-system command item
        if (!command.isSystemCommand()) {
            fields.imageView.setBackground(mActivity.getResources().getDrawable(R.drawable.transparent_button));
            fields.imageView.setOnClickListener(mActivity);
        }
        fields.imageView.setTag(new Integer(position));
        if (command.getCommandStart().equalsIgnoreCase(Command.OBEX_FTP_START)) {
            fields.playButton.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_file));
        } else {
            if (command.getStatus() == Command.Status.NOT_RUNNING) {
                fields.playButton.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_action_play));
            } else {
                fields.playButton.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_action_stop));
            }
        }

        fields.playButton.setOnClickListener(mActivity);
        fields.playButton.setTag(new Integer(position));

		return convertView;
	}
	
	private class FieldReferences {
        TextView commandName;
		TextView commandStart;
        TextView cpuUsage;
        ImageButton playButton;
        ImageView imageView;
	}
}
