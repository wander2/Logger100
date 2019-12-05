package park.haneol.project.logger.component;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import park.haneol.project.logger.R;
import park.haneol.project.logger.util.ActivityObserver;
import park.haneol.project.logger.util.Database;
import park.haneol.project.logger.util.PrefUtil;

// todo : 비밀 기능 추가 help 설명

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

                            // 데이터베이스, 어댑터 등 설정
                            mDatabase = new Database(HiddenActivity.this, Database.DATABASE_NAME_HIDDEN);
                            mAdapter.setItemList(mDatabase.load());
                            mAdapter.update(isSearchMode);

                            // 인풋 초기화
                            mInputText.getText().clear();

                            mode = 2;
                        } else {
                            // todo : 비밀번호가 일치하지 않습니다.
                            Toast.makeText(HiddenActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }

        // 객체 보존 취소
        ActivityObserver.getInstance().setActivity(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            mode = 0;
        }
    }
}
