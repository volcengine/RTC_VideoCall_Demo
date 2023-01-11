import { useEffect, useMemo } from 'react';
import { useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { RootState } from '@/store';
import Header from '@/components/Header';
import BottomMenu from './BottomMenu';
import PlayerWrapper from './PlayerWrapper';
import RoomInfo from './RoomInfo';

function View() {
  const navigate = useNavigate();
  const devices = useSelector((state: RootState) => state.device);
  const room = useSelector((state: RootState) => state.room);
  const isLocalUserShared = useMemo(
    () => room.localUser?.userId === room.shareUser,
    [room.localUser, room.shareUser]
  );

  useEffect(() => {
    if (!room.roomId) {
      navigate('/login');
    }
  }, [room.roomId, devices, navigate]);

  return (
    <>
      <Header hide={isLocalUserShared}>
        <RoomInfo />
      </Header>

      <PlayerWrapper />
      <BottomMenu />
    </>
  );
}

export default View;
