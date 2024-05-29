package com.example.notestakingapp.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notestakingapp.MainActivity;
import com.example.notestakingapp.R;
import com.example.notestakingapp.TodoFragment;
import com.example.notestakingapp.database.NoteComponent.ToDo;
import com.example.notestakingapp.ui.ToDoTest;
import com.example.notestakingapp.utils.AnimUtils;

import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
    List<ToDo> listTodo;
    TodoListener todoListener;

    public void setTodoListener(TodoListener todoListener) {
        this.todoListener = todoListener;
    }

    public void setTodos(List<ToDo> list) {
        this.listTodo = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo_main, parent, false);
        return new TodoViewHolder(view, todoListener);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        ToDo todo = listTodo.get(position);
        holder.setTodo(todo);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimatorSet animatorSet = AnimUtils.setAnim(v);
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(@NonNull Animator animation, boolean isReverse) {
                        super.onAnimationEnd(animation);
                        if (todo != null && todoListener != null)
                            todoListener.onItemClick(v, position, todo);
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        if (listTodo != null)
            return listTodo.size();
        return 0;
    }

    static class TodoViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView todoContent;

        TextView todoDate;
        TextView todoExpired;
        TodoListener todoListener;

        public TodoViewHolder(@NonNull View itemView, TodoListener todoListener) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.is_completed);
            todoContent = itemView.findViewById(R.id.todo_content);
            todoDate = itemView.findViewById(R.id.text_todo_date);
            todoExpired = itemView.findViewById(R.id.text_expired);
            this.todoListener = todoListener;
        }

        public void setTodo(ToDo todo) {
            if (todo != null) {
                //todo: setcheckbox nx
                todoContent.setText(todo.getContent());
                checkBox.setChecked(todo.isCompleted());
                if (todo.getDuration() != null && todo.getDuration()!=0) {
                    Log.d("duyTodoT","id="+todo.getId()+"dur: "+todo.getDuration() + "bool:"+String.valueOf(todo.getDuration()> System.currentTimeMillis()));
                    todoExpired.setVisibility(todo.getDuration() > System.currentTimeMillis() ? View.GONE : View.VISIBLE);
                } else {
                    todoExpired.setVisibility(View.GONE);
                }

                //su kien o checkbox
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (todoListener != null) {
                            todoListener.onCheckBoxClick(getAdapterPosition(), todo, isChecked);

                            Log.d("clickcheckbox", "@@@");
                        }
                    }
                });
            }
        }
    }

    public interface TodoListener {
        void onItemClick(View v, int position, ToDo todo);

        void onCheckBoxClick(int position, ToDo toDo, boolean isChecked);
    }
}
