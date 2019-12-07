package park.haneol.project.logger.component;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import park.haneol.project.logger.R;
import park.haneol.project.logger.item.LogItem;
import park.haneol.project.logger.util.ActivityObserver;
import park.haneol.project.logger.util.Database;
import park.haneol.project.logger.util.PrefUtil;
import park.haneol.project.logger.util.UIUtil;

// 1. 입력창 외에 아무 것도 안 나옴 (버튼:"확인")
// 2. 메뉴는 약간 변경됨 : 비밀 모드 진입 -> 비밀번호 변경 (비밀번호 입력하고 들어왔을 경우에만 유효)
// 3. 기본 비밀번호 ""를 입력하면 들어와짐, 비밀번호 변경 안내 문자 (잊어버릴 경우 되돌리기 불가능 경고)

public class HiddenActivity extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mode == 0) {
            mode = 1;
        }

        // 화면 보안
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        if (mode == 2 || isSearchMode) {
            // 진입 완료했다는 뜻

            // 데이터베이스, 어댑터 등 설정
            mDatabase = new Database(HiddenActivity.this, Database.DATABASE_NAME_HIDDEN);
            mAdapter.setItemList(mDatabase.load());
            mAdapter.update(isSearchMode);
        } else {
            // 어댑터 초기화
            mAdapter.setItemList(null);
            mAdapter.update(isSearchMode);

            // 입력창 초기화
            mInputText.setHint(R.string.msg_password_first);

            // 버튼 초기화
            mSaveButton.setText(R.string.confirm);
            final View.OnClickListener ocl = mSaveButton.getOnClickListener();
            final View.OnLongClickListener olcl = mSaveButton.getOnLongClickListener();
            mSaveButton.setOnLongClickListener(null);
            mSaveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mInputText.getText() != null) {
                        String text = mInputText.getText().toString();
                        if (text.equals(PrefUtil.getHiddenPassword(HiddenActivity.this))) {
                            // 버튼 되돌림
                            mSaveButton.setText(R.string.save);
                            mSaveButton.setOnClickListener(ocl);
                            mSaveButton.setOnLongClickListener(olcl);

                            // 입력창 되돌림
                            mInputText.setHint(null);

                            // 데이터베이스, 어댑터 등 설정
                            mDatabase = new Database(HiddenActivity.this, Database.DATABASE_NAME_HIDDEN);
                            mAdapter.setItemList(mDatabase.load());
                            mAdapter.update(isSearchMode);

                            // 인풋 초기화
                            mInputText.getText().clear();

                            mode = 2;
                        } else {
                            Toast.makeText(HiddenActivity.this, R.string.msg_password_not, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }

        // 객체 보존 취소
        ActivityObserver.getInstance().setActivity(null);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                UIUtil.keypadShown = false;
                UIUtil.fitCount = 2;
                UIUtil.predictMargin(mRootLayout, false);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            mode = 0;
        }
    }

    // 공유 받음
    void onTextSavedFromOutside(LogItem item) {
        // do nothing
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                UIUtil.keypadShown = false;
                UIUtil.fitCount = 2;
                UIUtil.predictMargin(mRootLayout, false);
            }
        }, 500);
    }
}
